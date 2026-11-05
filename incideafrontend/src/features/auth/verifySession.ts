import { supabase } from "#/lib/supabase"


export async function verifySession() {
    const { data, error } = await supabase.auth.getSession()
    if (error) throw error
    return data.session?.user ?? null
}